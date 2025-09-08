import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeletePersonnelComponent } from './delete-personnel.component';

describe('DeletePersonnelComponent', () => {
  let component: DeletePersonnelComponent;
  let fixture: ComponentFixture<DeletePersonnelComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DeletePersonnelComponent]
    });
    fixture = TestBed.createComponent(DeletePersonnelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
